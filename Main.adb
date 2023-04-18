with Ada.Text_IO; use Ada.Text_IO;
with Ada.Numerics.Float_Random;

procedure Main is

   Dim : constant Integer := 100;
   Thread_Num : constant Integer := 6;

   Final_Min : Integer := Dim;
   Index : Integer := 0;

   Arr : array (1..Dim) of Integer;

   procedure Init_Arr is
   begin
      for I in 1..Dim loop
         Arr(I) := I;
      end loop;
   end Init_Arr;

   function Part_Min (Start_Index, Finish_Index : in Integer) return Integer is
      Min : Integer := Dim;
   begin
      for I in Start_Index..Finish_Index loop
         if Arr(I) < Min then
            Min := Arr(I);
         end if;
      end loop;
      return Min;
   end Part_Min;

   task type Starter_Thread is
      entry Start (Start_Index, Finish_Index : in Integer);
   end Starter_Thread;

   protected Part_Manager is
      procedure Set_Part_Min (Min : in Integer);
      entry Get_Min (Min : out Integer);
   private
      Tasks_Count : Integer := 0;
      Part_Min : Integer := Dim;
   end Part_Manager;

   protected body Part_Manager is
      procedure Set_Part_Min (Min : in Integer) is
      begin
         if Part_Min > Min then
            Part_Min := Min;
         end if;

         Tasks_Count := Tasks_Count + 1;
      end Set_Part_Min;

      entry Get_Min (Min : out Integer) when Tasks_Count = Thread_Num is
      begin
         Min := Part_Min;
      end Get_Min;

   end Part_Manager;

   task body Starter_Thread is
      Min : Integer := Dim;
      Start_Index, Finish_Index : Integer;
   begin
      accept Start (Start_Index, Finish_Index : in Integer) do
         Starter_Thread.Start_Index := Start_Index;
         Starter_Thread.Finish_Index := Finish_Index;
      end Start;
      Min := Part_Min (Start_Index => Start_Index,
                       Finish_Index => Finish_Index);
      Part_Manager.Set_Part_Min (Min);
   end Starter_Thread;

   function Parallel_Min return Integer is
      Min : Integer := Dim;
      Thread : array (1..Thread_Num) of Starter_Thread;
      Part_Dim : Integer := Dim / Thread_Num;
      Rnd : Ada.Numerics.Float_Random.Generator;
      Index : Integer;
   begin
      Ada.Numerics.Float_Random.Reset (Rnd);
      Index := Integer (Ada.Numerics.Float_Random.Random (Rnd) * Float (Dim)) + 1;
      Arr (Index) := -10;
      for I in 1..Thread_Num loop
         if I = Thread_Num then
            Thread (I).Start (Part_Dim * (I - 1) + 1, Dim);
         else
            Thread (I).Start (Part_Dim * (I - 1) + 1, Part_Dim * I);
         end if;
      end loop;
      Part_Manager.Get_Min (Min);
      return Min;
   end Parallel_Min;

begin
   Init_Arr;
   Final_Min := Parallel_Min;
   for I in Arr'Range loop
      if Arr (I) = Final_Min then
         Index := I;
exit;
end if;
   end loop;
   Put_Line ("The minimum element is " & Final_Min'Img & " and its index is " & Index'Img);
End Main;